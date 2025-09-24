FROM python:3.11-slim

RUN apt-get update && apt-get install -y ffmpeg

WORKDIR /app

COPY sh/ffmpeg/transcode.py .

CMD ["python", "transcode.py"]