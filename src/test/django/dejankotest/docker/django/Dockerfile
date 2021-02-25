FROM python:3.9
ENV PYTHONUNBUFFERED 1

ENV PUID=${PUID:-1000}
ENV PGID=${PGID:-1000}

RUN addgroup --gid $PGID django \
  && useradd -u $PUID -g $PGID django

# install dev dependencys
RUN apt-get update \
  && apt-get install -y --no-install-recommends gdal-bin

# create and activate our virtual env
COPY requirements/ /requirements
RUN pip install --no-cache-dir -r /requirements/local.txt \
  && rm -rf /requirements

USER django

WORKDIR /code

CMD ["python", "/code/manage.py", "runserver", "0.0.0.0:8000"]