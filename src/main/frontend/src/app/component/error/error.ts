import {Component, Input} from '@angular/core';
import {Error} from '../../model/error';

@Component({
    selector: 'app-error',
    templateUrl: './error.html',
    styleUrls: ['./error.css']
})
export class ErrorComponent {
    @Input() error: Error;

    message(): string {
        let msg = '';
        if (this.error.httpStatus > 0) {
            msg += ('HTTP: ' + this.error.httpStatus);
        }
        msg += (' ' + this.error.message);
        return msg;
    }
}
