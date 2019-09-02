import {Component, OnInit} from '@angular/core';
import {StorageService} from '../../../service/storage';
import {ActivatedRoute, Params} from '@angular/router';
import {Error} from '../../../model/error';
import {RestService} from '../../../service/rest';
import {FormControl, FormGroup, Validators} from '@angular/forms';

@Component({
    selector: 'app-subscribe',
    templateUrl: './subscribe.html',
    styleUrls: ['./subscribe.css']
})
export class SubscribeComponent implements OnInit {

    subscriptionForm: FormGroup;

    showForm = true;
    showMessage = false;
    message = '';
    email: string;
    error: Error;

    constructor(public storage: StorageService, private restService: RestService, private activatedRoute: ActivatedRoute) {
    }

    submit() {
        console.log('submit');
        if (this.subscriptionForm.status === 'INVALID') {
            this.subscriptionForm.controls['email'].markAsTouched();
        } else {
            this.email = this.subscriptionForm.controls['email'].value;
            this.restService.subscribe(this.email).subscribe(
                model => {
                    this.error = null;
                    this.showForm = false;
                    this.message = model.value;
                    this.showMessage = true;
                },
                error => {
                    this.error = new Error(0, error.error && error.error.message ? error.error.message : error.message);
                    this.error.header = '';
                }
            );
        }
    }

    ngOnInit(): void {

        this.subscriptionForm = new FormGroup({
            email: new FormControl('', [Validators.required, Validators.email]),
        });

        this.activatedRoute.queryParams.subscribe((params: Params) => {
            const hash = params['hash'];
            const email = params['email'];
            const check = params['check'];
            if (hash && hash.length > 0) {
                this.restService.confirm(hash).subscribe(
                    model => {
                        this.error = null;
                        this.showForm = false;
                        this.message = model.value;
                        this.showMessage = true;
                    },
                    error => {
                        this.error = new Error(0, error.error && error.error.message ? error.error.message : error.message);
                        this.error.header = '';
                    }
                );
            } else if (email && email.length > 0 && check && check.length > 0) {
                this.restService.unsubscribe(email, check).subscribe(
                    model => {
                        this.error = null;
                        this.showForm = false;
                        this.message = model.value;
                        this.showMessage = true;
                    },
                    error => {
                        this.error = new Error(0, error.error && error.error.message ? error.error.message : error.message);
                        this.error.header = '';
                    }
                );
            }
        });
    }

    formGroupEnter($event) {
        $event.preventDefault();
        this.submit();
    }
}
