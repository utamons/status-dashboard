import {User} from './user';

export class Session {
    user: User;
    createdAt: Date;
    token: string;
}
