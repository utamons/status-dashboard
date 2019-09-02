import {Component} from '@angular/core';
import {StorageService} from '../../../service/storage';
import {Router} from '@angular/router';
import {RestService} from '../../../service/rest';

@Component({
    selector: 'app-menu',
    templateUrl: './menu.html',
    styleUrls: ['./menu.css']
})
export class MenuComponent {
    constructor(public storage: StorageService, private restService: RestService, private router: Router) {
    }

    public logout(): void {
        // todo Implement session expiration with warning popup.
        // todo Implement extending session time after every user request.

        this.restService.logout(this.storage.sessionId).subscribe(
            () => {}
        );

        this.storage.clearAll();
        this.router.navigate(['/home']).catch();
    }
}
