FROM python:3.13-slim

WORKDIR /app
COPY local-server /app/local-server

EXPOSE 8765
CMD ["python3", "/app/local-server/src/aocam_server/main.py", "--host", "0.0.0.0", "--port", "8765"]
