import {Component, OnInit} from '@angular/core';
import {StorageService} from '../../../service/storage';
import {ServiceStatus} from '../../../model/serviceStatus';
import {ServiceAnnouncement} from '../../../model/serviceAnnouncement';
import {RestService} from '../../../service/rest';
import {Error} from '../../../model/error';
import {Page} from '../../../model/page';
import {ConfirmationPopupComponent} from '../confirmation/confirmation';
import {MatDialog} from '@angular/material';


@Component({
    selector: 'app-main',
    templateUrl: './main.html',
    styleUrls: ['./main.css']
})

export class MaintainerComponent implements OnInit {

    issueButtonDisabled = false;
    annoButtonDisabled = false;
    showIssueForm = false;
    showAnnouncementForm = false;
    currentStatus: ServiceStatus;
    currentAnnouncement: ServiceAnnouncement;
    error: Error = null;
    isNewIssue = true;
    isNewAnnouncement = true;

    issueReportButtonDisabled = false;
    issueReportPage: Page = null;
    showIssueReportPage = false;
    issueReportsButtonText = 'User Reports';
    issueReportsBadge = '';
    issueReportsBadgeStyle: string;

    constructor(public storage: StorageService,  private restService: RestService, public dialog: MatDialog) {
        this.currentStatus = storage.serviceStatus;
        this.currentAnnouncement = storage.announcement;
    }

    newIssue() {
        this.showIssueForm = true;
        this.annoButtonDisabled = true;
        this.issueButtonDisabled = true;
        this.issueReportButtonDisabled = true;
        this.isNewIssue = true;
        this.issueReportsBadge = '';
        this.currentStatus = new ServiceStatus(this.storage.serviceStatus.components);
    }

    updateIssue() {
        this.showIssueForm = true;
        this.annoButtonDisabled = true;
        this.issueButtonDisabled = true;
        this.issueReportButtonDisabled = true;
        this.isNewIssue = false;

        this.currentStatus = this.storage.serviceStatus;
    }

    newAnnouncement() {
        this.showAnnouncementForm = true;
        this.annoButtonDisabled = true;
        this.issueButtonDisabled = true;
        this.issueReportButtonDisabled = true;
        this.isNewAnnouncement = true;
        this.issueReportsBadge = '';
        this.currentAnnouncement = null;
    }

    updateAnnouncement() {
        this.showAnnouncementForm = true;
        this.annoButtonDisabled = true;
        this.issueButtonDisabled = true;
        this.issueReportButtonDisabled = true;
        this.isNewAnnouncement = false;
        this.issueReportsBadge = '';
        this.currentAnnouncement = this.storage.announcement;
    }

    cancelIssueForm() {
        this.showIssueForm = false;
        this.annoButtonDisabled = false;
        this.issueButtonDisabled = false;
        this.issueReportButtonDisabled = false;
        this.issueReportsBadge = '' + this.issueReportPage.totalElements;
    }

    submitIssueForm(status) {
        console.log('Status ', status);
        if (this.isNewIssue) {
            this.restService.postStatus(status, this.storage.sessionId).subscribe(
                backStatus => {
                    this.storage.serviceStatus = ServiceStatus.fromBackend(backStatus);
                    this.currentStatus = this.storage.serviceStatus;
                    this.error = null;
                    this.showIssueForm = false;
                    this.issueButtonDisabled = false;
                    this.annoButtonDisabled = false;
                    this.issueReportButtonDisabled = false;
                    this.issueReportsBadge = '' + this.issueReportPage.totalElements;
                },
                error => {
                    this.error = new Error(0, error.error && error.error.message ? error.error.message : error.message);
                }
            );
        } else {
            this.restService.putStatus(status, this.storage.sessionId).subscribe(
                backStatus => {
                    this.storage.serviceStatus = ServiceStatus.fromBackend(backStatus);
                    this.currentStatus = this.storage.serviceStatus;
                    this.error = null;
                    this.showIssueForm = false;
                    this.issueButtonDisabled = false;
                    this.annoButtonDisabled = false;
                    this.issueReportButtonDisabled = false;
                    this.issueReportsBadge = '' + this.issueReportPage.totalElements;
                },
                error => {
                    this.error = new Error(0, error.error && error.error.message ? error.error.message : error.message);
                }
            );
        }
    }

    isActiveIssue() {
        return this.currentStatus ? this.currentStatus.isActiveIssue() : false;
    }

    resolveIssue() {
        const dialogRef = this.dialog.open(ConfirmationPopupComponent, {
            data: {
                text: 'Resolve the issue?',
                confirmButton: 'Yes',
                rejectButton: 'No'
            }
        });
        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.restService.resolveStatus(this.storage.serviceStatus.id, this.storage.sessionId).subscribe(
                    status => {
                        this.storage.serviceStatus = ServiceStatus.fromBackend(status);
                        this.currentStatus = this.storage.serviceStatus;
                        this.error = null;
                        this.showIssueForm = false;
                        this.issueButtonDisabled = false;
                        this.annoButtonDisabled = false;
                        this.issueReportButtonDisabled = false;
                    },
                    error => {
                        this.error = new Error(0, error.error && error.error.message ? error.error.message : error.message);
                    }
                );
            }
        });
    }

    submitAnnouncementForm(announcement) {
        if (this.isNewAnnouncement) {
            this.restService.postAnnouncement(announcement, this.storage.sessionId).subscribe(
                backAnnouncement => {
                    this.storage.announcement = backAnnouncement;
                    this.error = null;
                    this.showAnnouncementForm = false;
                    this.annoButtonDisabled = false;
                    this.issueButtonDisabled = false;
                    this.issueReportButtonDisabled = false;
                    this.issueReportsBadge = '' + this.issueReportPage.totalElements;
                },
                error => {
                    this.error = new Error(0, error.error && error.error.message ? error.error.message : error.message);
                }
            );
        } else {
            this.restService.putAnnouncement(announcement, this.storage.sessionId).subscribe(
                backAnnouncement => {
                    this.storage.announcement = backAnnouncement;
                    this.error = null;
                    this.showAnnouncementForm = false;
                    this.annoButtonDisabled = false;
                    this.issueButtonDisabled = false;
                    this.issueReportButtonDisabled = false;
                    this.issueReportsBadge = '' + this.issueReportPage.totalElements;
                },
                error => {
                    this.error = new Error(0, error.error && error.error.message ? error.error.message : error.message);
                }
            );
        }
    }

    cancelAnnouncementForm() {
        this.showAnnouncementForm = false;
        this.annoButtonDisabled = false;
        this.issueButtonDisabled = false;
        this.issueReportButtonDisabled = false;
        this.issueReportsBadge = '' + this.issueReportPage.totalElements;
    }

    hasAnnouncement() {
        return this.storage.announcement != null;
    }

    deleteAnnouncement() {
        const dialogRef = this.dialog.open(ConfirmationPopupComponent, {
            data: {
                text: 'Delete the announcement?',
                confirmButton: 'Yes',
                rejectButton: 'No'
            }
        });
        dialogRef.afterClosed().subscribe(result => {
            if (result) {
                this.restService.deleteAnnouncement(this.storage.announcement.id, this.storage.sessionId).subscribe(
                    model => {
                        this.storage.announcement = null;
                        this.showAnnouncementForm = false;
                        this.annoButtonDisabled = false;
                        this.issueButtonDisabled = false;
                        this.issueReportButtonDisabled = false;
                        console.log('Deleting model - ', model.value);
                    },
                    error => {
                        this.error = new Error(0, error.error && error.error.message ? error.error.message : error.message);
                    }
                );
            }
        });
    }

    ngOnInit(): void {
        this.restService.getStatus().subscribe(
            status => {
                this.storage.serviceStatus = ServiceStatus.fromBackend(status);
                this.currentStatus = this.storage.serviceStatus;
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
        this.restService.getIssueReports(0, 100, false, this.storage.sessionId).subscribe(
            page => {
                this.issueReportPage = page;
                this.error = null;
                this.issueReportsBadge = '' + this.issueReportPage.totalElements;
                this.issueReportsBadgeStyle = 'margin-right: ' + (this.issueReportsBadge.length > 0 ? 17 : 0) + 'px';
            },
            error => {
                this.error = new Error(0, error.error && error.error.message ? error.error.message : error.message);
            }
        );
    }

    isIssueReports() {
        return this.issueReportPage != null && this.issueReportPage.content && this.issueReportPage.content.length > 0;
    }

    issueReports() {
        this.showIssueReportPage = !this.showIssueReportPage;
        if (this.showIssueReportPage) {
            this.issueReportsButtonText = 'Hide Reports';
            this.issueReportsBadge = '';
            this.issueReportsBadgeStyle = 'margin-right: ' + (this.issueReportsBadge.length > 0 ? 17 : 0) + 'px';
        } else {
            this.issueReportsButtonText = 'User Reports';
            this.issueReportsBadge = '' + this.issueReportPage.totalElements;
            this.issueReportsBadgeStyle = 'margin-right: 0';
        }
    }

    reloadIssueReports() {
        this.restService.getIssueReports(0, 100, false, this.storage.sessionId).subscribe(
            page => {
                this.issueReportPage = page;
                this.error = null;
                this.issueReportsBadge = '' + this.issueReportPage.totalElements;
                this.issueReportsBadgeStyle = 'margin-right: ' + (this.issueReportsBadge.length > 0 ? 17 : 0) + 'px';
                this.showIssueReportPage = this.issueReportPage.totalElements > 0;
            },
            error => {
                this.error = new Error(0, error.error && error.error.message ? error.error.message : error.message);
            }
        );
    }
}
