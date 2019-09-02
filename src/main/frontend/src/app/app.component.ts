import {Component} from '@angular/core';
import {NavigationEnd, Router} from '@angular/router';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent {
    constructor(private router: Router) {
        this.router.events.subscribe((e) => {
            if (e instanceof NavigationEnd) {
                if (e.url.indexOf('?') > -1) {
                    const query = e.url.split('?')[1].split('&');
                    const params = {};
                    let route = '';
                    query.forEach((p) => {
                        const chunk = p.split('=');
                        if (chunk[0] === 'route') {
                            route = chunk[1];
                        } else {
                            params[chunk[0]] = chunk[1];
                        }
                    });

                    if (route !== '') {
                        route = '/' + route;
                        // noinspection JSIgnoredPromiseFromCall
                        this.router.navigate([route], {queryParams: params});
                    }
                }
            }
        });
    }
}
