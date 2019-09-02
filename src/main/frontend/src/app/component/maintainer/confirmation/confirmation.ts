import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA} from '@angular/material';

export interface ConfirmationPopupData {
    text: string;
    confirmButton: string;
    rejectButton: string;
}

@Component({
    selector: 'app-confirmation',
    templateUrl: './confirmation.html',
})
export class ConfirmationPopupComponent {

    constructor(@Inject(MAT_DIALOG_DATA) public data: ConfirmationPopupData ) {}
}

