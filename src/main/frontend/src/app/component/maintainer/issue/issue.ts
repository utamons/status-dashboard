import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {ServiceStatus} from '../../../model/serviceStatus';
import {EventUpdate} from '../../../model/eventUpdate';
import {FormControl, FormGroup, Validators} from '@angular/forms';

@Component({
    selector: 'app-issue',
    templateUrl: './issue.html',
    styleUrls: ['./issue.css'],
})
export class IssueFormComponent implements OnInit {

    issueForm: FormGroup;

    @Output() submitEvent: EventEmitter<ServiceStatus> = new EventEmitter();
    @Output() cancelEvent: EventEmitter<void> = new EventEmitter();
    selectedComponents: string[] = [];
    componentsNotValid = false;

    @Input() currentStatus: ServiceStatus = new ServiceStatus([]);
    issueMessageType: string;
    issueMessage: string;
    buttonText: string;

    ngOnInit() {


        if (this.isActiveIssue()) {
            this.buttonText = 'Save Issue';
            this.issueForm = new FormGroup({
                statusType: new FormControl(this.currentStatus.statusType, [Validators.required]),
                statusString: new FormControl(this.currentStatus.statusString, [Validators.required]),
                description: new FormControl(this.currentStatus.description, [Validators.required]),
                currentEventStatusString: new FormControl(this.currentStatus.currentEvent.statusString, [Validators.required]),
                currentEventDescription: new FormControl(this.currentStatus.currentEvent.description, [Validators.required])
            });
            this.populateSelectedComponents();
        } else {
            this.issueForm = new FormGroup({
                statusType: new FormControl(this.currentStatus.statusType, [Validators.required]),
                statusString: new FormControl(this.currentStatus.statusString, [Validators.required]),
                description: new FormControl(this.currentStatus.description, [Validators.required]),
                currentEventStatusString: new FormControl(this.currentStatus.currentEvent.statusString, [Validators.required]),
                currentEventDescription: new FormControl(this.currentStatus.currentEvent.description, [Validators.required]),
                issueMessageType: new FormControl(this.issueMessageType, [Validators.required]),
                issueMessage: new FormControl(this.issueMessage, [Validators.required])
            });
            this.buttonText = 'Submit Issue';
        }
    }

    submitIssue() {
        const currentStatus = this.currentStatus;
        const form = this.issueForm;
        const currentEvent = currentStatus.currentEvent;
        const formControls = form.controls;

        this.componentsNotValid = this.selectedComponents.length === 0;

        if (form.status === 'INVALID' || this.componentsNotValid) {
            form.markAsTouched();
        } else {

            if (!this.isActiveIssue()) {
                currentEvent.history.push(new EventUpdate(new Date(),
                    formControls['issueMessageType'].value,
                    formControls['issueMessage'].value));
            }

            currentStatus.statusString = formControls['statusString'].value;
            currentStatus.statusType = formControls['statusType'].value;
            currentStatus.description = formControls['description'].value;
            currentEvent.statusString = formControls['currentEventStatusString'].value;
            currentEvent.description = formControls['currentEventDescription'].value;
            currentEvent.eventType = currentStatus.statusType;
            this.fillStatusWithComponents(currentStatus.statusType);
            currentEvent.components = currentStatus.components;
            this.submitEvent.emit(currentStatus);
        }
    }

    cancelIssue() {
        this.cancelEvent.emit();
    }

    selectComponents(selection) {
        this.selectedComponents = [];
        selection.selectedOptions.selected.forEach(sel => {
            this.selectedComponents.push(sel._text.nativeElement.innerText);
        });
        this.componentsNotValid = this.selectedComponents.length === 0;
    }

    private populateSelectedComponents() {
        this.currentStatus.components.forEach(c => {
            if (c.statusType !== 'normal') {
                this.selectedComponents.push(c.name);
            }
        });
    }

    private fillStatusWithComponents(statusType) {
        for (let i = 0; i < this.currentStatus.components.length; ++i) {

            this.currentStatus.components[i].statusString = 'operational';
            this.currentStatus.components[i].statusType = 'normal';

            for (let j = 0; j < this.selectedComponents.length; ++j) {
                if (this.selectedComponents[j] === this.currentStatus.components[i].name) {
                    this.currentStatus.components[i].statusString = 'unavailable';
                    this.currentStatus.components[i].statusType = statusType;
                    break;
                }
            }
        }
    }

    isActiveIssue(): boolean {
        return this.currentStatus.isActiveIssue();
    }

    getUpdateEvents(updates) {
        console.log('Got updates: ', updates, this.currentStatus);
    }
}
