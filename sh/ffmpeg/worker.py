                    import os
                    from kafka import KafkaConsumer
                    import subprocess
                    import json

                    # 환경변수 읽기
                    KAFKA_BROKER = os.getenv("KAFKA_BROKER", "kafka:9092")
                    KAFKA_TOPIC = os.getenv("KAFKA_TOPIC", "video-tasks")
                    MINIO_ENDPOINT = os.getenv("MINIO_ENDPOINT", "minio:9000")
                    MINIO_ACCESS_KEY = os.getenv("MINIO_ACCESS_KEY", "minioadmin")
                    MINIO_SECRET_KEY = os.getenv("MINIO_SECRET_KEY", "minioadmin")

                    # Kafka Consumer 생성
                    consumer = KafkaConsumer(
                        KAFKA_TOPIC,
                        bootstrap_servers=[KAFKA_BROKER],
                        value_deserializer=lambda m: json.loads(m.decode('utf-8')),
                        auto_offset_reset='earliest',
                        enable_auto_commit=True
                    )

                    print("FFmpeg Worker Kafka 구독 시작...")

                    for message in consumer:
                        task = message.value
                        input_file = task['video_file']
                        output_file = task.get('output_file', f'processed_{input_file}')
                        resolution = task.get('resolution', '1280x720')

                        # MinIO에서 영상 다운로드 (mc 필요)
                        subprocess.run([
                            "mc", "alias", "set", "localminio", f"http://{MINIO_ENDPOINT}", MINIO_ACCESS_KEY, MINIO_SECRET_KEY
                        ])
                        subprocess.run([
                            "mc", "cp", f"localminio/videos/{input_file}", f"/videos/{input_file}"
                        ])

                        # FFmpeg 처리
                        subprocess.run([
                            "ffmpeg", "-i", f"/videos/{input_file}", "-vf", f"scale={resolution}", f"/videos/{output_file}"
                        ])

                        # MinIO 업로드
                        subprocess.run([
                            "mc", "cp", f"/videos/{output_file}", f"localminio/processed/{output_file}"
                        ])

                        print(f"[완료] {input_file} → {output_file}")
