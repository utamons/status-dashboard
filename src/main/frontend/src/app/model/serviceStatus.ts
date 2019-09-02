import {ServiceEvent} from './serviceEvent';
import {ServiceComponent} from './serviceComponent';
import {toLongString} from '../util/dateTime';
import {EventUpdate} from './eventUpdate';

const ACTIVE_STATES: string [] = ['partial', 'accident', 'maintenance'];

export class ServiceStatus {


    constructor(components: ServiceComponent[]) {
        this.components = components;
    }
    id: number;
    MAX_HISTORY_SIZE = 5;
    date: Date = new Date();
    statusString = '';
    description = '';
    statusType = '';
    currentEvent: ServiceEvent = new ServiceEvent();

    components: ServiceComponent [] = [];

    history: ServiceEvent [] = [];

    private static getNormalComponents(): ServiceComponent [] {
        return [
            new ServiceComponent('Tax'),
            new ServiceComponent('Credit Card Processing'),
            new ServiceComponent('Vendor Gateway'),
            new ServiceComponent('PayPal Gateway'),
            new ServiceComponent('Merchant Care'),
            new ServiceComponent('Self Care'),
        ];
    }

    static fromBackend(status): ServiceStatus {
        const serviceStatus = new ServiceStatus(status.components);
        serviceStatus.id = status.id;
        serviceStatus.statusType = status.statusType;
        serviceStatus.statusString = status.statusString;
        serviceStatus.description = status.description;
        serviceStatus.currentEvent = status.currentEvent ? ServiceEvent.fromBackend(status.currentEvent, status.components) : null;
        serviceStatus.history = ServiceStatus.historyFromBackend(status);
        console.log('status.updatedAt - ', status.updatedAt);
        serviceStatus.date = status.updatedAt ? new Date(status.updatedAt) : new Date();
        return serviceStatus;
    }

    private static historyFromBackend(status) {
        const result = [];
        if (status.history) {
            status.history.forEach(h => {
                result.push(ServiceEvent.fromBackend(h, status.components));
            });
        }
        return result;
    }

    dateStr(): string {
        return toLongString(this.date);
    }


    isActiveIssue(): boolean {
        return ACTIVE_STATES.some(s => {
            return s === this.statusType;
        });
    }

    isResolved(): boolean {
        return this.currentEvent.history[this.currentEvent.history.length - 1].type === 'Resolved';
    }

    resolve() {
        if (!this.isResolved() && this.statusType !== 'maintenance') {
            this.currentEvent.history.push(new EventUpdate(new Date(), 'Resolved', 'The issue is now resolved.'));
        }
        this.addToHistory(this.currentEvent);
        this.date = new Date();
        this.statusString = 'Service is operational';
        this.description = 'Welcome to the Service Status Page. There you can see current information of the service performance. ' +
            'You can bookmark or subscribe to this page for the latest updates.';
        this.statusType = 'normal';
        this.components = ServiceStatus.getNormalComponents();
        this.currentEvent = null;
    }

    private addToHistory(event: ServiceEvent) {
        if (this.history.length >= this.MAX_HISTORY_SIZE) {
            this.history = this.history.slice(0, this.MAX_HISTORY_SIZE - 1);
        }
        this.history.unshift(event);
    }
}
