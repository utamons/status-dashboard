import {Component, OnInit} from '@angular/core';
import {StorageService} from '../../../service/storage';
import {Router} from '@angular/router';
import {RestService} from '../../../service/rest';
import {Error} from '../../../model/error';
import {User} from '../../../model/user';
import {FormControl, FormGroup, Validators} from '@angular/forms';

@Component({
    selector: 'app-login',
    templateUrl: './login.html',
    styleUrls: ['./login.css']
})
export class LoginComponent implements OnInit {

    loginForm: FormGroup;
    user: User = new User();
    error: Error;

    constructor(public storage: StorageService, private restService: RestService, private router: Router ) {}


    submit() {
        if (this.loginForm.status === 'INVALID') {
            this.loginForm.controls['username'].markAsTouched();
            this.loginForm.controls['password'].markAsTouched();
        } else {
            this.user = this.loginForm.getRawValue();
            this.restService.login(this.user).subscribe(
                session => {
                    this.storage.sessionId = session.token;
                    this.storage.role = session.user.role;
                    if (session.user.role === 'maintainer') {
                        this.router.navigate(['/maintain']).catch();
                    }
                },
                error => {
                    if (error.status === 403) {
                        this.error = new Error(0, 'Wrong username or password.');
                    } else {
                        this.error = new Error(0, error.error && error.error.message ? error.error.message : error.message);
                    }

                    this.error.header = '';
                }
            );
        }

    }

    ngOnInit(): void {
        this.loginForm = new FormGroup({
            username: new FormControl('', [Validators.required]),
            password: new FormControl('', [Validators.required]),
        });
    }

    formGroupEnter($event) {
        $event.preventDefault();
        this.submit();
    }
}
