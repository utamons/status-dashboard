import {Component, OnInit} from '@angular/core';
import {StorageService} from '../../../service/storage';
import {RestService} from '../../../service/rest';
import {ServiceStatus} from '../../../model/serviceStatus';
import {Error} from '../../../model/error';

@Component({
    selector: 'app-history',
    templateUrl: './history.html',
    styleUrls: ['./history.css']
})
export class HistoryComponent implements OnInit {
    constructor(public storage: StorageService, private restService: RestService) {
    }

    error: Error = null;

    ngOnInit(): void {
        this.restService.getStatus().subscribe(
            status => {
                this.storage.serviceStatus = ServiceStatus.fromBackend(status);
                this.error = null;
            },
            error => {
                this.error = new Error(0, error.error && error.error.message ? error.error.message : error.message);
            }
        );
    }
}
