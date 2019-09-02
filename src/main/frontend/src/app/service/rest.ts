import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';

import {tap} from 'rxjs/operators';
import {Observable} from 'rxjs';
import {User} from '../model/user';
import {Session} from '../model/session';
import {ServiceStatus} from '../model/serviceStatus';
import {ServiceAnnouncement} from '../model/serviceAnnouncement';
import {Model} from '../model/model';
import {IssueReport} from '../model/issueReport';
import {Page} from '../model/page';

@Injectable()
export class RestService {

    private url = 'https://localhost:8443/api/';  // URL to web api

    constructor(private http: HttpClient) {
    }

    getStatus(): Observable<{}> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json'
            })
        };
        return this.http.get<{}>(this.url + 'status', httpOptions).pipe(
            tap(() => console.log(`got status`))
        );
    }

    getAnnouncement(): Observable<ServiceAnnouncement> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json'
            })
        };
        return this.http.get<ServiceAnnouncement>(this.url + 'announcement', httpOptions).pipe(
            tap(() => console.log(`got announcement`))
        );
    }

    postStatus(status: ServiceStatus, sessionId: string): Observable<{}> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                'session-id': sessionId
            })
        };
        return this.http.post<ServiceStatus>(this.url + 'status', status, httpOptions).pipe(
            tap(() => console.log(`sent new status`))
        );
    }


    postAnnouncement(announcement: ServiceAnnouncement, sessionId: string): Observable<ServiceAnnouncement> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                'session-id': sessionId
            })
        };
        return this.http.post<ServiceAnnouncement>(this.url + 'announcement', announcement, httpOptions).pipe(
            tap(() => console.log(`sent new announcement`))
        );
    }

    postIssueReport(issueReport: IssueReport): Observable<Model> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json'
            })
        };
        return this.http.post<Model>(this.url + 'issueReport', issueReport, httpOptions).pipe(
            tap(() => console.log(`added new report`))
        );
    }

    putIssueReport(issueReport: IssueReport, sessionId: string): Observable<Model> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                'session-id': sessionId
            })
        };
        return this.http.put<Model>(this.url + 'issueReport', issueReport, httpOptions).pipe(
            tap(() => console.log(`updated a report`))
        );
    }

    getIssueReports(startPage: number, pageSize: number, showProcessed: boolean, sessionId: string): Observable<Page> {
        const httpOptions = {
            headers: new HttpHeaders({
                'session-id': sessionId
            })
        };
        return this.http.get<Page>(this.url + 'issueReport?start=' + startPage + '&size='
            + pageSize + '&processed=' + showProcessed,
            httpOptions).pipe(
            tap(() => console.log(`got new reports page`))
        );
    }

    putAnnouncement(announcement: ServiceAnnouncement, sessionId: string): Observable<ServiceAnnouncement> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                'session-id': sessionId
            })
        };
        return this.http.put<ServiceAnnouncement>(this.url + 'announcement', announcement, httpOptions).pipe(
            tap(() => console.log(`sent new announcement`))
        );
    }

    deleteAnnouncement(id: number, sessionId: string): Observable<Model> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                'session-id': sessionId
            })
        };
        return this.http.delete<Model>(this.url + 'announcement/' + id, httpOptions).pipe(
            tap(() => console.log(`deleted announcement`))
        );
    }

    resolveStatus(id: number, sessionId: string): Observable<{}> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                'session-id': sessionId
            })
        };
        return this.http.post<{}>(this.url + 'resolve/' + id, {}, httpOptions).pipe(
            tap(() => console.log(`resolved status`))
        );
    }

    putStatus(status: ServiceStatus, sessionId: string): Observable<{}> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                'session-id': sessionId
            })
        };
        return this.http.put<ServiceStatus>(this.url + 'status', status, httpOptions).pipe(
            tap(() => console.log(`sent new status`))
        );
    }

    login(user: User): Observable<Session> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json'
            })
        };
        return this.http.post<Session>(this.url + 'login', user,  httpOptions).pipe(
            tap((v: Session) => console.log(`got session=${v.token}`))
        );
    }

    subscribe(email: string): Observable<Model> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json'
            })
        };
        return this.http.post<Model>(this.url + 'subscription?email=' + email,  httpOptions).pipe(
            tap(() => console.log(`got subscription`))
        );
    }

    confirm(hash: string): Observable<Model> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json'
            })
        };
        return this.http.post<Model>(this.url + 'confirm?hash=' + hash,  httpOptions).pipe(
            tap(() => console.log(`got confirmation`))
        );
    }

    unsubscribe(email: string, check: string): Observable<Model> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json'
            })
        };
        return this.http.delete<Model>(this.url + 'subscription?email=' + email + '&check=' + check,  httpOptions).pipe(
            tap(() => console.log(`delete subscription`))
        );
    }

    logout(sessionId: string): Observable<Model> {
        const httpOptions = {
            headers: new HttpHeaders({
                'Content-Type': 'application/json',
                'session-id': sessionId
            })
        };
        return this.http.delete<Model>(this.url + 'login',  httpOptions).pipe(
            tap(() => console.log(`logout`))
        );
    }
}
