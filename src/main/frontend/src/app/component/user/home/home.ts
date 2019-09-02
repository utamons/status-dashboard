import {Component, OnDestroy, OnInit} from '@angular/core';
import {RestService} from '../../../service/rest';
import {StorageService} from '../../../service/storage';
import {Subscription, timer} from 'rxjs';
import {Error} from '../../../model/error';
import {ServiceStatus} from '../../../model/serviceStatus';

@Component({
    selector: 'app-home',
    templateUrl: './home.html',
    styleUrls: ['./home.css']
})
export class MainComponent implements OnInit, OnDestroy {

    error: Error = null;
    private pollForStatus: Subscription;

    constructor(private restService: RestService, public storage: StorageService) {}

    loggedIn(): boolean {
        const result: boolean = this.storage.loggedIn();
        if (!result) {
            this.storage.error = 'not authenticated';
        }
        return result;
    }

    ngOnInit(): void {

        const tm = timer(300, 10000);
        this.pollForStatus = tm.subscribe(() => {
            this.restService.getStatus().subscribe(
                status => {
                    this.storage.serviceStatus = ServiceStatus.fromBackend(status);
                    this.error = null;
                },
                error => {
                    this.error = new Error(0, error.error && error.error.message ? error.error.message : error.message);
                }
            );
            this.restService.getAnnouncement().subscribe(
                announcement => {
                    this.storage.announcement = announcement;
                    this.error = null;
                },
                error => {
                    this.error = new Error(0, error.error && error.error.message ? error.error.message : error.message);
                }
            );
        });

    }

    isActiveIssue(): boolean {
        return this.storage.serviceStatus && this.storage.serviceStatus.isActiveIssue();
    }

    ngOnDestroy(): void {
        if (this.pollForStatus) {
            this.pollForStatus.unsubscribe();
        }
    }
}
