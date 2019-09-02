import {Component, EventEmitter, Input, Output} from '@angular/core';
import {EventUpdate} from '../../../model/eventUpdate';

@Component({
    selector: 'app-updates',
    templateUrl: './updates.html',
    styleUrls: ['./updates.css']
})
export class EventUpdatesComponent {

    @Output() updatesEvent: EventEmitter<EventUpdate[]> = new EventEmitter();

    @Input() updates: EventUpdate[] = [];
    showForm = false;
    formMessageType = '';
    formMessage = '';
    rowMessageType = '';
    rowMessage = '';
    updatingRow = -1;

    formOpen() {
        this.formMessageType = '';
        this.formMessage = '';
        this.showForm = true;
    }

    formCancel() {
        this.showForm = false;
        this.formMessageType = '';
        this.formMessage = '';
    }

    formSave() {
        console.log('this.formMessageType - ', this.formMessageType, ', this.formMessage - ', this.formMessage);
        this.showForm = false;
        if (this.formMessageType !== '' && this.formMessage.trim() !== '') {
            this.updates.push(new EventUpdate(new Date(), this.formMessageType, this.formMessage));
            this.updatesEvent.emit(this.updates);
        }
        console.log('updates - ', this.updates.length);
    }

    updateRow(i) {
        this.updatingRow = i;
        this.rowMessage = this.updates[i].message;
        this.rowMessageType = this.updates[i].type;
    }

    deleteRow(i) {
        this.updates.splice(i, 1);
        this.updatesEvent.emit(this.updates);
    }

    saveRow() {
        if (this.rowMessageType !== '' && this.rowMessage.trim() !== '') {
            this.updates[this.updatingRow].message = this.rowMessage;
            this.updates[this.updatingRow].type = this.rowMessageType;
            this.updatesEvent.emit(this.updates);
        }
        this.rowMessage = '';
        this.rowMessageType = '';
        this.updatingRow = -1;
    }

    cancelRow() {
        this.updatingRow = -1;
        this.rowMessage = '';
        this.rowMessageType = '';
    }
}
