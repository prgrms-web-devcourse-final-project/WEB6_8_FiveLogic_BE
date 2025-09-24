FROM python:3.11-slim

RUN apt-get update && apt-get install -y ffmpeg libsasl2-dev libssl-dev python3-dev

RUN pip install confluent-kafka

WORKDIR /app

COPY sh/ffmpeg/transcode.py .

CMD ["python", "transcode.py"]