If the texture fails to load, it is probably because of a CORS error (security model null) because you tried to execute the script from a local session, not from the web. 
Host it on AWS S3 or anywhere else for better results.

See: https://stackoverflow.com/questions/41965066/access-to-image-from-origin-null-has-been-blocked-by-cors-policy
