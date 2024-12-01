#!/bin/bash
 
# renew certbot certificate
docker compose -f /home/csw/csw/docker-compose.yml run --rm certbot
docker compose -f /home/csw/csw/docker-compose.yml exec nginx nginx -s reload
