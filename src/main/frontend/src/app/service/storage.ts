import {Injectable} from '@angular/core';
import {ServiceStatus} from '../model/serviceStatus';
import {ServiceAnnouncement} from '../model/serviceAnnouncement';

@Injectable()
export class StorageService {

    private _sessionId;
    private _role;
    private _error;
    private _serviceStatus: ServiceStatus;
    private _announcement: ServiceAnnouncement;

    constructor() {
        this._sessionId = sessionStorage.getItem('sessionId');
        this._role = sessionStorage.getItem('role');
        this._error = '';
        this._serviceStatus = null;
        console.log('Session id = ', this._sessionId);
    }

    clearAll() {
        sessionStorage.removeItem('sessionId');
        sessionStorage.removeItem('role');
        this._error = '';
        this._role = '';
        this._sessionId = '';
    }

    get announcement(): ServiceAnnouncement {
        return this._announcement;
    }

    set announcement(value: ServiceAnnouncement) {
        this._announcement = value;
    }

    get serviceStatus(): ServiceStatus {
        return this._serviceStatus;
    }

    set serviceStatus(value: ServiceStatus) {
        this._serviceStatus = value;
    }

    get error() {
        return this._error;
    }

    set error(value) {
        this._error = value;
    }


    get sessionId() {
        if (!this._sessionId) {
            this._sessionId = sessionStorage.getItem('sessionId');
        }
        return this._sessionId;
    }

    set sessionId(value) {
        this._sessionId = value;
        sessionStorage.setItem('sessionId', value);
    }


    get role() {
        if (!this._role) {
            this._role = sessionStorage.getItem('role');
        }
        return this._role;
    }

    set role(value) {
        this._role = value;
        sessionStorage.setItem('role', value);
    }

    loggedIn() {
        return this._sessionId != null && this._sessionId.length > 0;
    }
}
