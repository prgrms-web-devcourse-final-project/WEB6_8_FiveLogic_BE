from confluent_kafka import Consumer, KafkaException, KafkaError
import sys
import os

# Kafka Consumer 설정
conf = {
    'bootstrap.servers': 'host.docker.internal:9092',
    'group.id': 'transcoder-group',
    'auto.offset.reset': 'earliest'
}

# Consumer 인스턴스 생성
try:
    consumer = Consumer(conf)
except KafkaException as e:
    sys.stderr.write(f"Kafka consumer creation failed: {e}\n")
    sys.exit(1)

# 구독할 토픽 지정
topic = 's3-events'
consumer.subscribe([topic])

print(f"[{topic}] 토픽 메시지를 기다리는 중... (Ctrl+C로 종료)")

# 메시지 폴링 루프
try:
    while True:
        msg = consumer.poll(1.0)  # 1초마다 메시지 폴링

        if msg is None:
            continue
        if msg.error():
            # 에러 처리
            if msg.error().code() == KafkaError.PARTITION_EOF:
                # 토픽 끝에 도달
                continue
            else:
                print(f"Consumer error: {msg.error()}")
                break

        # 메시지 수신 성공
        print(f"메시지 수신: {msg.value().decode('utf-8')}")

except KeyboardInterrupt:
    pass
finally:
    # Consumer 종료
    consumer.close()
    print("Consumer 종료.")