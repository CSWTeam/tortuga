How to setup a local test environment

1. If you do not have node installed, install it.
2. Clone this repository

    git clone https://github.com/LionC/rms4csw.git

3. In the root folder of the project (if you did not specify anything else while cloning it is named rms4csw) run

    npm install

4. Configure your local webserver to serve the application. Keep in mind that the site uses hTML5 history states, so all requests to nonexisting files have to be redirected to `index.html`.An example configuration for [`nginx`]() is

  worker_processes  1;

  events {
      worker_connections  1024;
  }


  http {
      server {
          include /usr/local/etc/nginx/mime.types;

          #The port the website should be served on
          listen 2222;

          #Some name. Is only used for nginx logging
          server_name bp;

          location / {
            #The root folder of the project on your disk
            root /Users/LionC/Projects/rms4csw;

            try_files $uri $uri/ /index.html;
          }
      }
  }


5. Build the website by running

    gulp build
