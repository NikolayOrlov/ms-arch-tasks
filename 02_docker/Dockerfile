FROM python:3.12-slim
# Install curl
RUN apt-get update && apt-get upgrade && apt-get install -y curl
# Set the working directory for RUN, CMD, ENTRYPOINT, COPY and ADD commands
WORKDIR /usr/src/app

# Copy service artifacts into the container at WORKDIR
ADD ./src .
ADD ./requirements.txt ./requirements.txt

# Install any needed packages specified in requirements.txt
RUN python3 -m pip install --trusted-host pypi.python.org -r requirements.txt

# Make port available to the world outside this container
EXPOSE 8000

# Run app.py when the container launches
ENTRYPOINT ["gunicorn", "--bind", "0.0.0.0:8000", "--worker-class", "gevent", "--worker-connections", "30", "--timeout", "60", "app:app"]

HEALTHCHECK --start-period=30s --interval=5s CMD curl http://localhost:8000/health || exit 1
