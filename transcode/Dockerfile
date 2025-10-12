FROM python:3.11-slim

WORKDIR /app

COPY requirements.txt .
RUN apt-get update && apt-get install -y ffmpeg
RUN pip install --no-cache-dir -r requirements.txt

COPY consumer.py .

ENV KAFKA_TOPIC=s3-events
ENV KAFKA_BOOTSTRAP=kafka:29092
ENV DOWNLOAD_DIR=/downloads

VOLUME ["/downloads"]

CMD ["python", "consumer.py"]
