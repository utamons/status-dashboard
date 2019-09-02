import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {FormControl, FormGroup, Validators} from '@angular/forms';
import {ServiceAnnouncement} from '../../../model/serviceAnnouncement';

@Component({
    selector: 'app-announcement-form',
    templateUrl: './announcementForm.html',
    styleUrls: ['./announcementForm.css'],
})
export class AnnouncementFormComponent implements OnInit {

    announcementForm: FormGroup;
    buttonText: string;

    @Output() public submitEvent: EventEmitter<ServiceAnnouncement> = new EventEmitter();
    @Output() cancelEvent: EventEmitter<void> = new EventEmitter();

    @Input() announcement: ServiceAnnouncement = null;

    ngOnInit() {
        if (this.announcement == null) {
            this.buttonText = 'Submit';
            this.announcement = new ServiceAnnouncement();
        } else {
            this.buttonText = 'Save';
        }
        this.announcementForm = new FormGroup({
            header: new FormControl(this.announcement.header, [Validators.required]),
            description: new FormControl(this.announcement.description, [Validators.required]),
        });
    }

    submitAnnouncement() {
        if (this.announcementForm.status === 'INVALID') {
            this.announcementForm.markAsTouched();
        } else {
            const id = this.announcement.id;
            this.announcement = this.announcementForm.getRawValue();
            this.announcement.id = id;
            this.submitEvent.emit(this.announcement);
        }
    }

    cancelAnnouncement() {
        this.cancelEvent.emit();
    }
}
