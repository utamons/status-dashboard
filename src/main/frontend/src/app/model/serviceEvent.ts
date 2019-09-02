import {EventUpdate} from './eventUpdate';
import {ServiceComponent} from './serviceComponent';
import {toShortString} from '../util/dateTime';

export class ServiceEvent {

    constructor(date: Date = new Date(),
                statusString: string = '',
                description: string = '',
                eventType: string = '',
                components: ServiceComponent[] = [],
                history: EventUpdate[] = []) {
        this.date = date;
        this.statusString = statusString;
        this.description = description;
        this.eventType = eventType;
        this.history = history;
        this.components = components;
    }

    id: number;
    date: Date = new Date();
    statusString = '';
    description = '';
    eventType = '';
    history: EventUpdate [] = [];
    components: ServiceComponent [] = [];

    static fromBackend(serviceEvent, components): ServiceEvent {
        const history: EventUpdate [] = [];
        serviceEvent.history.forEach(h => {
            const update = new EventUpdate(
                new Date(h.date),
                h.type,
                h.message
            );
            update.id = h.id;
            history.push(
                update
            );
        });
        const event = new ServiceEvent(
            new Date(serviceEvent.eventDate),
            serviceEvent.statusString,
            serviceEvent.description,
            serviceEvent.eventType,
            components,
            history
        );
        event.id = serviceEvent.id;
        return event;
    }

    public componentsString(): string {
        let result = '';

        if (this.eventType !== 'normal' && this.components.length > 0) {
            this.components.forEach(c => {
                if (c.statusType !== 'normal' && result.length === 0) {
                    result += c.name;
                } else if (c.statusType !== 'normal') {
                    result += (', ' + c.name);
                }
            });
        }

        return result;
    }

    public dateShortStr(): string {
        return toShortString(this.date);
    }
}
