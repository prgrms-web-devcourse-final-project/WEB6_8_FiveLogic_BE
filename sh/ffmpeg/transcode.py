import subprocess
import os

input_video = "input.mp4"
output_video = "output.mp4"

# 입력 파일이 있는지 확인
if not os.path.exists(input_video):
    print(f"오류: '{input_video}' 파일을 찾을 수 없습니다. 스크립트와 같은 폴더에 넣어주세요.")
    exit()

print(f"'{input_video}' 파일 트랜스코딩 시작...")

try:
    command = [
        "ffmpeg",
        "-i", input_video,      # 입력 파일
        "-c:v", "libx264",      # 비디오 코덱: H.264
        "-crf", "23",           # Constant Rate Factor (0: 무손실, 51: 최저 품질)
        "-preset", "medium",    # 인코딩 속도/압축률 균형 (ultrafast ~ slow)
        "-c:a", "aac",          # 오디오 코덱: AAC
        "-b:a", "128k",         # 오디오 비트레이트
        output_video            # 출력 파일
    ]

    # subprocess.run을 사용하여 외부 명령어 실행
    # check=True: 명령어가 오류 없이 종료되면 True, 아니면 CalledProcessError 발생
    process = subprocess.run(command, check=True, capture_output=True, text=True)

    print(f"트랜스코딩 성공! 결과 파일: '{output_video}'")
    print("\n--- ffmpeg 출력 ---")
    print(process.stdout)
    print("-------------------")

except subprocess.CalledProcessError as e:
    print(f"트랜스코딩 실패: {e.returncode}")
    print("stderr:", e.stderr)
except FileNotFoundError:
    print("오류: 'ffmpeg' 명령어를 찾을 수 없습니다. ffmpeg가 제대로 설치되었는지 확인하세요.")
except Exception as e:
    print(f"예상치 못한 오류 발생: {e}")