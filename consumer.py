import os
import json
import time
import subprocess
from kafka import KafkaConsumer, KafkaProducer
from kafka.errors import NoBrokersAvailable
import boto3

# =========================
# 환경 변수 설정
# =========================

# Kafka
KAFKA_TOPIC = os.getenv("KAFKA_TOPIC", "s3-events")
KAFKA_TRANSCODING_STATUS_TOPIC = os.getenv("KAFKA_TRANSCODING_STATUS_TOPIC", "transcoding-status")
KAFKA_BOOTSTRAP = os.getenv("KAFKA_BOOTSTRAP", "kafka:29092")  # Docker 내부 네트워크용
GROUP_ID = os.getenv("GROUP_ID", "minio-consumer")

# S3 / MinIO
MINIO_ENDPOINT = os.getenv("MINIO_ENDPOINT", "http://minio:9000")
MINIO_ACCESS_KEY = os.getenv("MINIO_ACCESS_KEY", "minioadmin")
MINIO_SECRET_KEY = os.getenv("MINIO_SECRET_KEY", "minioadmin")
DOWNLOAD_DIR = os.getenv("DOWNLOAD_DIR", "/downloads")
REUPLOAD_BUCKET = os.getenv("REUPLOAD_BUCKET", "transcoded-videos")

# 다운로드 디렉토리 생성
os.makedirs(DOWNLOAD_DIR, exist_ok=True)

# =========================
# S3 클라이언트 생성
# =========================
s3 = boto3.client(
    "s3",
    endpoint_url=MINIO_ENDPOINT,
    aws_access_key_id=MINIO_ACCESS_KEY,
    aws_secret_access_key=MINIO_SECRET_KEY
)

# =========================
# Kafka Producer 연결 재시도
# =========================
while True:
    try:
        producer = KafkaProducer(
            bootstrap_servers=KAFKA_BOOTSTRAP,
            value_serializer=lambda v: json.dumps(v).encode('utf-8')
        )
        print("Kafka Producer 연결 성공...")
        break
    except NoBrokersAvailable:
        print("Kafka Producer 연결 실패, 5초 후 재시도...")
        time.sleep(5)

# =========================
# Kafka 메시지 전송 함수
# =========================
def send_kafka_message(producer, topic, bucket, key, qualities_status):
    message = {
        "bucket": bucket,
        "key": key,
        "qualities": qualities_status
    }
    producer.send(topic, message)
    producer.flush()
    print(f"Kafka 메시지 전송: {json.dumps(message)}")


# =========================
# DASH 트랜스코딩 함수
# =========================
def is_video(file_path):
    try:
        cmd = [
            "ffprobe",
            "-v", "error",
            "-select_streams", "v:0",
            "-show_entries", "stream=codec_type",
            "-of", "default=noprint_wrappers=1:nokey=1",
            file_path
        ]
        output = subprocess.check_output(cmd, stderr=subprocess.STDOUT).decode().strip()
        return output == "video"
    except Exception:
        return False

def encode_dash_multi_quality(input_file, output_dir, producer, topic, bucket, key):
    os.makedirs(output_dir, exist_ok=True)

    qualities = [
        ("1080p", "1920x1080", "5000k"),
        ("720p",  "1280x720",  "3000k"),
        ("480p",  "854x480",   "1500k")
    ]

    qualities_status = {
        q[0]: {"file_path": None, "status": "PENDING", "size_mb": 0} for q in qualities
    }

    # 트랜스코딩 시작 메시지
    send_kafka_message(producer, topic, bucket, key, qualities_status)

    for name, resolution, bitrate in qualities:
        quality_dir = os.path.join(output_dir, name)
        os.makedirs(quality_dir, exist_ok=True)
        manifest_path = os.path.join(quality_dir, "manifest.mpd")

        # 특정 화질 트랜스코딩 진행중
        qualities_status[name]["status"] = "IN_PROGRESS"
        send_kafka_message(producer, topic, bucket, key, qualities_status)

        cmd = [
            "ffmpeg",
            "-i", input_file,
            "-c:v", "libx264",
            "-b:v", bitrate,
            "-s", resolution,
            "-c:a", "aac",
            "-f", "dash",
            manifest_path
        ]

        print(f"{name} 트랜스코딩 시작: {input_file} → {quality_dir}")
        try:
            subprocess.run(cmd, check=True)
            print(f"{name} 트랜스코딩 완료: {quality_dir}")
            
            # 트랜스코딩 완료
            qualities_status[name]["status"] = "COMPLETED"
            qualities_status[name]["file_path"] = f"{key}/{name}/manifest.mpd"
            
            # DASH 폴더의 모든 파일 크기 합산
            total_size_bytes = 0
            for root, _, files in os.walk(quality_dir):
                for file in files:
                    total_size_bytes += os.path.getsize(os.path.join(root, file))
            qualities_status[name]["size_mb"] = round(total_size_bytes / (1024 * 1024), 2)

            send_kafka_message(producer, topic, bucket, key, qualities_status)

        except subprocess.CalledProcessError as e:
            print(f"{name} 트랜스코딩 실패: {e}")
            qualities_status[name]["status"] = "FAILED"
            send_kafka_message(producer, topic, bucket, key, qualities_status)

    # 모든 화질에 대한 처리가 끝났음을 알리는 최종 메시지
    print("전체 트랜스코딩 과정 완료.")
    send_kafka_message(producer, topic, bucket, key, qualities_status)


# =========================
# DASH 폴더 업로드 함수
# =========================
def upload_folder_to_minio(local_folder, bucket_name, s3_prefix=""):
    try:
        s3.head_bucket(Bucket=bucket_name)
    except:
        s3.create_bucket(Bucket=bucket_name)
        print(f"버킷 생성: {bucket_name}")

    for root, dirs, files in os.walk(local_folder):
        for file in files:
            local_path = os.path.join(root, file)
            relative_path = os.path.relpath(local_path, local_folder)
            s3_key = os.path.join(s3_prefix, relative_path).replace("\\", "/")
            print(f"업로드 중: {local_path} → {bucket_name}/{s3_key}")
            s3.upload_file(local_path, bucket_name, s3_key)
    print(f"폴더 업로드 완료: {bucket_name}/{s3_prefix}")

# =========================
# Kafka Consumer 연결 재시도
# =========================
while True:
    try:
        consumer = KafkaConsumer(
            KAFKA_TOPIC,
            bootstrap_servers=KAFKA_BOOTSTRAP,
            auto_offset_reset='earliest',
            enable_auto_commit=True,
            group_id=GROUP_ID
        )
        print("Kafka 이벤트 구독 시작...")
        break
    except NoBrokersAvailable:
        print("Kafka 브로커 연결 실패, 5초 후 재시도...")
        time.sleep(5)

# =========================
# 이벤트 처리 루프
# =========================
for msg in consumer:
    try:
        data = json.loads(msg.value.decode('utf-8'))
        bucket = data['Records'][0]['s3']['bucket']['name']
        key = data['Records'][0]['s3']['object']['key']
        print(f"업로드 감지: {bucket}/{key}")

        # 다운로드
        download_path = os.path.join(DOWNLOAD_DIR, key.replace("/", "_"))
        s3.download_file(bucket, key, download_path)
        print(f"다운로드 완료: {download_path}")

        # 영상 확인 후 DASH 인코딩 (3화질)
        if is_video(download_path):
            object_key_without_ext = os.path.splitext(key)[0]
            dash_output_dir = os.path.join(DOWNLOAD_DIR, "dash_" + object_key_without_ext)
            
            encode_dash_multi_quality(
                download_path, 
                dash_output_dir, 
                producer, 
                KAFKA_TRANSCODING_STATUS_TOPIC, 
                bucket, 
                object_key_without_ext
            )
            print(f"DASH 인코딩 완료: {dash_output_dir}")

            # DASH 결과 재업로드
            upload_folder_to_minio(dash_output_dir, REUPLOAD_BUCKET, s3_prefix=object_key_without_ext)
        else:
            print(f"영상 아님, 인코딩 스킵: {download_path}")

    except Exception as e:
        print("오류:", e)
