export class ServiceComponent {

    constructor (name: string) {
        this.name = name;
    }

    id: number;
    name = '';
    statusString = 'operational';
    statusType = 'normal';
}
