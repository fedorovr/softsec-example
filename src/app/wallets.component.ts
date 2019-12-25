import {Component, OnInit}from '@angular/core';
import {AppService}from './app.service';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import {Router}from '@angular/router';

@Component({
templateUrl: './wallets.component.html'
})
export class WalletsComponent {

wallets = [{name: "name111", secret: "secret123"}];
fileToUpload: File = null;

constructor(private app: AppService, private http: HttpClient, private router: Router) {
    console.log(this.app.authenticated);
    this.wallets = [{name: "name111", secret: "secret123"}];
    if (this.app.authenticated) {
      let token = this.app.getToken();
        let body = new URLSearchParams();
          body.append('token', token);
          let options = {headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')};
          this.http.post('/getWallets', body.toString(), options).subscribe(response => {
              if (response['wallets']) {
                this.wallets = response['wallets'];
              }
          });
    }
  }

  getWallets() {
    if (this.app.authenticated) {
      let token = this.app.getToken();
      let body = new URLSearchParams();
      body.append('token', token);
      let options = {headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')};
      this.http.post('/getWallets', body.toString(), options).subscribe(response => {
        if (response['wallets']) {
          this.wallets = response['wallets'];
        }
      });
    }
  }

  addWallet() {
    if (this.app.authenticated) {
      let token = this.app.getToken();
      let body = new URLSearchParams();
      body.append('token', token);
      let options = {headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')};
      this.http.post('/createWallet', body.toString(), options).subscribe(response => {
        if (response['wallets']) {
          this.wallets = response['wallets'];
        }
      });
    }
  }

  authenticated() { return this.app.authenticated; }

  export() {
    console.log('export started 2');
    if (this.app.authenticated) {
      let token = this.app.getToken();
        let body = new URLSearchParams();
          body.append('token', token);
          let options = {headers: new HttpHeaders().set('Content-Type', 'application/x-www-form-urlencoded')};
          this.http.post('/exportWallets', body.toString(), options).subscribe(response => {

          });
    }
  }

  handleFileInput(files) {
    this.fileToUpload = files.item(0);
  }

  postFile(fileToUpload) {
    const endpoint = '/importWallets';
    const formData: FormData = new FormData();
    formData.append('token', this.app.getToken());
    formData.append('file', fileToUpload, fileToUpload.name);
    return this.http.post(endpoint, formData, { headers: new HttpHeaders() })
      .subscribe(response => {
        this.getWallets();
      })
  }

  uploadFileToActivity() {
    this.postFile(this.fileToUpload)
      // .subscribe(data => {
      // // do something, if upload success
      // }, error => {
      //   console.log(error);
      // });
  }

  //uploadFile(event) {
  //  let files = event.target.files;
  //  if (files.length > 0) {
  //    console.log(file); // You will see the file

//      let formData: FormData = new FormData();
  //    formData.append('file', file, file.name);
    //  formData.append('token', this.app.getToken());
   //   let options = {headers: new HttpHeaders().set('Content-Type', 'multipart/form-data')};
     // this.http.post('/importWallets', formData, options);
    //}
  //}
}
