import os
import json
import time
import subprocess
from kafka import KafkaConsumer
from kafka.errors import NoBrokersAvailable
import boto3

# Kafka 설정
KAFKA_TOPIC = os.getenv("KAFKA_TOPIC", "s3-events")
KAFKA_BOOTSTRAP = os.getenv("KAFKA_BOOTSTRAP", "kafka:29092")  # Docker 내부 네트워크용
GROUP_ID = os.getenv("GROUP_ID", "minio-consumer")

# MinIO 설정
MINIO_ENDPOINT = os.getenv("MINIO_ENDPOINT", "http://minio:9000")
MINIO_ACCESS_KEY = os.getenv("MINIO_ACCESS_KEY", "minioadmin")
MINIO_SECRET_KEY = os.getenv("MINIO_SECRET_KEY", "minioadmin")
DOWNLOAD_DIR = os.getenv("DOWNLOAD_DIR", "/downloads")
REUPLOAD_BUCKET = os.getenv("REUPLOAD_BUCKET", "transcoded-videos")

# 다운로드 디렉토리 생성
os.makedirs(DOWNLOAD_DIR, exist_ok=True)

# MinIO 클라이언트
s3 = boto3.client(
    "s3",
    endpoint_url=MINIO_ENDPOINT,
    aws_access_key_id=MINIO_ACCESS_KEY,
    aws_secret_access_key=MINIO_SECRET_KEY
)

# DASH 폴더 재귀 업로드
def upload_folder_to_minio(local_folder, bucket_name, s3_prefix=""):
    try:
        # 버킷 없으면 생성
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

# 영상 확인
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

# DASH 인코딩
def encode_dash(input_file, output_dir):
    os.makedirs(output_dir, exist_ok=True)
    cmd = [
        "ffmpeg",
        "-i", input_file,
        "-c:v", "libx264",
        "-c:a", "aac",
        "-f", "dash",
        os.path.join(output_dir, "manifest.mpd")
    ]
    subprocess.run(cmd, check=True)

# Kafka Consumer 연결 재시도
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

# 이벤트 처리
for msg in consumer:
    try:
        data = json.loads(msg.value.decode('utf-8'))
        bucket = data['Records'][0]['s3']['bucket']['name']
        key = data['Records'][0]['s3']['object']['key']
        print(f"업로드 감지: {bucket}/{key}")

        # 파일 다운로드
        download_path = os.path.join(DOWNLOAD_DIR, key.replace("/", "_"))
        s3.download_file(bucket, key, download_path)
        print(f"다운로드 완료: {download_path}")

        # 영상 확인 후 DASH 인코딩
        if is_video(download_path):
            dash_output_dir = os.path.join(DOWNLOAD_DIR, "dash_" + os.path.splitext(key)[0])
            encode_dash(download_path, dash_output_dir)
            print(f"DASH 인코딩 완료: {dash_output_dir}")

            # DASH 결과 폴더 재업로드
            upload_folder_to_minio(dash_output_dir, REUPLOAD_BUCKET, s3_prefix=os.path.splitext(key)[0])
        else:
            print(f"영상 아님, 인코딩 스킵: {download_path}")

    except Exception as e:
        print("오류:", e)
