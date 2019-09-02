import {toShortString} from '../util/dateTime';

export class EventUpdate {

    id: number;
    date: Date = null;
    type = '';
    message = '';

    constructor(date: Date, type: string, message: string) {
        this.date = date;
        this.type = type;
        this.message = message;
    }

    get dateString(): string {
        return toShortString(this.date);
    }
}
