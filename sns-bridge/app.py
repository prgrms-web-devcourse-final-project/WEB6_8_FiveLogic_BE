from flask import Flask, request, jsonify
from kafka import KafkaProducer
import json
import os
import time
import requests # 자동 구독 확인을 위해 requests 라이브러리 추가

app = Flask(__name__)

# =========================
# 환경 변수 및 초기화
# =========================

KAFKA_BOOTSTRAP = os.getenv("KAFKA_BOOTSTRAP", "kafka:29092")
KAFKA_TOPIC = os.getenv("KAFKA_TOPIC", "s3-events")

# Kafka Producer 연결 재시도
while True:
    try:
        producer = KafkaProducer(
            bootstrap_servers=[KAFKA_BOOTSTRAP],
            value_serializer=lambda v: json.dumps(v).encode("utf-8")
        )
        print("Kafka Producer 연결 성공...")
        break
    except Exception as e:
        print(f"Kafka Producer 연결 실패, 5초 후 재시도... 오류: {e}")
        time.sleep(5)


@app.route("/sns", methods=["POST"])
def sns_listener():
    # force=True 옵션으로 Content-Type이 application/json이 아니어도 JSON으로 파싱 시도
    data = request.get_json(force=True)

    # =========================================================
    # 1. SNS 인증 요청 (SubscriptionConfirmation) - 자동 확인 로직
    # =========================================================
    if "Type" in data and data["Type"] == "SubscriptionConfirmation":
        print("SNS SubscriptionConfirmation received. Initiating auto-confirmation.")
        subscribe_url = data.get('SubscribeURL')

        if not subscribe_url:
            print("❌ SubscribeURL이 메시지에 없습니다. 수동 확인이 필요할 수 있습니다.")
            return jsonify({"message": "Subscription confirmation received, but URL missing"}), 200

        try:
            # SubscribeURL로 GET 요청을 보내 구독을 자동으로 확인합니다.
            response = requests.get(subscribe_url)
            # 2xx 응답이 아니면 예외 발생
            response.raise_for_status()
            print(f"✅ Subscription Confirmed successfully! URL: {subscribe_url}")
            return jsonify({"message": "Subscription automatically confirmed"}), 200
        except requests.exceptions.RequestException as e:
            print(f"❌ ERROR confirming subscription: {e}")
            # 자동 확인 실패하더라도 500 에러를 반환하여 SNS가 재시도할 수 있도록 합니다.
            return jsonify({"message": "Subscription confirmation failed"}), 500

    # =========================================================
    # 2. 실제 S3 이벤트 메시지 (Notification)
    # =========================================================
    if "Type" in data and data["Type"] == "Notification":
        try:
            # SNS 메시지 본문은 문자열 JSON이므로 다시 파싱해야 함
            message = json.loads(data["Message"])
            print(f"📦 Received S3 event: {json.dumps(message, indent=2)}")

            # Kafka로 S3 이벤트 메시지 전송
            producer.send(KAFKA_TOPIC, message)
            producer.flush()

            return jsonify({"status": "sent to kafka"}), 200
        except Exception as e:
            print(f"❌ ERROR processing notification: {e}")
            return jsonify({"status": "processing failed"}), 500

    # =========================================================
    # 3. 기타 메시지 타입 (UnsubscribeConfirmation 등)
    # =========================================================
    print(f"🤷 Ignored SNS message type: {data.get('Type')}")
    return jsonify({"status": "ignored"}), 200


if __name__ == "__main__":
    port = int(os.getenv("SNS_PORT", 5000))
    # Flask 서버 실행
    app.run(host="0.0.0.0", port=port)