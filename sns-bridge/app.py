from flask import Flask, request, jsonify
from kafka import KafkaProducer
import json
import os

app = Flask(__name__)

KAFKA_BOOTSTRAP = os.getenv("KAFKA_BOOTSTRAP", "kafka:29092")
KAFKA_TOPIC = os.getenv("KAFKA_TOPIC", "s3-events")

producer = KafkaProducer(
    bootstrap_servers=[KAFKA_BOOTSTRAP],
    value_serializer=lambda v: json.dumps(v).encode("utf-8")
)

@app.route("/sns", methods=["POST"])
def sns_listener():
    data = request.get_json(force=True)

    # SNS 인증 요청 (SubscriptionConfirmation)
    if "Type" in data and data["Type"] == "SubscriptionConfirmation":
        print("SNS SubscriptionConfirmation received")
        print(f"Confirm URL: {data['SubscribeURL']}")
        return jsonify({"message": "Subscription confirmation received"}), 200

    # 실제 S3 이벤트 메시지
    if "Type" in data and data["Type"] == "Notification":
        message = json.loads(data["Message"])
        print(f"📦 Received S3 event: {json.dumps(message, indent=2)}")
        producer.send(KAFKA_TOPIC, message)
        producer.flush()
        return jsonify({"status": "sent to kafka"}), 200

    return jsonify({"status": "ignored"}), 200


if __name__ == "__main__":
    port = int(os.getenv("SNS_PORT", 5000))
    app.run(host="0.0.0.0", port=port)
