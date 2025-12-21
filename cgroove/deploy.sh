#!/bin/bash

echo "🚀 빌드 시작! (Build)"
./gradlew bootJar

echo "📦 파일 보내는 중... (Upload)"
# 키 파일 경로랑 IP 확인해주세요!
scp -i ~/Documents/cgroove-key.pem build/libs/cgroove-0.0.1-SNAPSHOT.jar ec2-user@13.209.43.137:/home/ec2-user/app/

echo "🔥 서버 재시작! (Restart)"
ssh -i ~/Documents/cgroove-key.pem ec2-user@13.209.43.137 "sudo systemctl restart cgroove"

echo "✅ 배포 완료! 수고하셨습니다!"
