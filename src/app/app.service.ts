import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { URLSearchParams } from '@angular/http';

@Injectable()
export class AppService {

  authenticated = false;
  token = null;

  constructor(private http: HttpClient) {
  }

  getToken() {
    if (!this.authenticated) {
      return undefined;
    } else {
      return this.token;
    }
  }

  authenticate(credentials, callback) {
        if (credentials && credentials.username && credentials.password) {
          let body = new URLSearchParams();
          body.append('login', credentials.username);
          body.append('password', credentials.password);
          //console.log(body);
          let options = {headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')};
          this.http.post('/login', body.toString(), options).subscribe(response => {
              if (response['token']) {
                  this.authenticated = true;
                  this.token = response['token'];
              } else {
                  this.authenticated = false;
                  this.token = null;
              }
              return callback && callback();
          });
        } else {
          this.authenticated = false;
          this.token = null;
        }
    }

}
