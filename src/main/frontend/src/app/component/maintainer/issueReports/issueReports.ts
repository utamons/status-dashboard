import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Page} from '../../../model/page';
import {RestService} from '../../../service/rest';
import {Error} from '../../../model/error';
import {StorageService} from '../../../service/storage';
import {MatDialog} from '@angular/material';
import {ConfirmationPopupComponent} from '../confirmation/confirmation';

@Component({
    selector: 'app-i-reports',
    templateUrl: './issueReports.html',
    styleUrls: ['./issueReports.css']
})
/**
 * Work with users issue reports
 */
export class IssueReportsComponent {
    @Input() reports: Page;
    updatingRow = -1;
    error: Error;
    @Output() updateEvent: EventEmitter<void> = new EventEmitter();
    remarkOld: string;

    constructor(public storage: StorageService, private restService: RestService, public dialog: MatDialog) {
    }

    archive(i: number) {
        const dialogRef = this.dialog.open(ConfirmationPopupComponent, {
            data: {
                text: 'The report will be archived, are you sure?',
                confirmButton: 'Yes',
                rejectButton: 'No'
            }
        });
        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.updatingRow = -1;
                this.reports.content[i].processed = true;
                this.update(i);
            }
        });
    }

    add(i: number) {
        this.updatingRow = i;
        this.remarkOld = this.reports.content[i].remarkText;
    }

    save(i: number) {
        this.updatingRow = -1;
        this.update(i);
    }

    update(i: number) {
        this.restService.putIssueReport(this.reports.content[i], this.storage.sessionId).subscribe(
            model => {
                console.log('got ', model.value);
                this.updateEvent.emit();
            },
            error => {
                this.error = new Error(0, error.error && error.error.message ? error.error.message : error.message);
            }
        );
    }

    cancel(i: number) {
        this.updatingRow = -1;
        this.reports.content[i].remarkText = this.remarkOld;
    }
}
