import os
import json
import time
from kafka import KafkaConsumer
from kafka.errors import NoBrokersAvailable
import boto3

# Kafka 설정
KAFKA_TOPIC = os.getenv("KAFKA_TOPIC", "s3-events")
KAFKA_BOOTSTRAP = os.getenv("KAFKA_BOOTSTRAP", "kafka:29092")  # 내부 네트워크용
GROUP_ID = os.getenv("GROUP_ID", "minio-consumer")

# MinIO 설정
MINIO_ENDPOINT = os.getenv("MINIO_ENDPOINT", "http://minio:9000")
MINIO_ACCESS_KEY = os.getenv("MINIO_ACCESS_KEY", "minioadmin")
MINIO_SECRET_KEY = os.getenv("MINIO_SECRET_KEY", "minioadmin")
DOWNLOAD_DIR = os.getenv("DOWNLOAD_DIR", "/downloads")

# 다운로드 디렉토리 생성
os.makedirs(DOWNLOAD_DIR, exist_ok=True)

# MinIO 클라이언트
s3 = boto3.client(
    "s3",
    endpoint_url=MINIO_ENDPOINT,
    aws_access_key_id=MINIO_ACCESS_KEY,
    aws_secret_access_key=MINIO_SECRET_KEY
)

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
        # 이벤트 구조에 따라 Bucket과 Object 키 확인
        bucket = data['Records'][0]['s3']['bucket']['name']
        key = data['Records'][0]['s3']['object']['key']
        print(f"업로드 감지: {bucket}/{key}")

        # 파일 다운로드
        download_path = os.path.join(DOWNLOAD_DIR, key.replace("/", "_"))
        s3.download_file(bucket, key, download_path)
        print(f"다운로드 완료: {download_path}")

    except Exception as e:
        print("오류:", e)
