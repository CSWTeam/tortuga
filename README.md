# TorTUga

## Deployment

- Build Docker:
  
  `docker-compose build`

- Import dump:

  ```bash
  docker exec -i csw-postgres-1 bash -c 'psql -U postgres -d postgres -c "DROP DATABASE IF EXISTS rms4csw;" && psql -U postgres -d postgres -c "CREATE DATABASE rms4csw;" && psql -U postgres -d rms4csw' < /home/csw/db.dump
  ```

- Export dump:

  ```bash
  docker exec -i csw-postgres-1 bash -c 'pg_dump -U postgres rms4csw' > /home/db.dump
  ```
