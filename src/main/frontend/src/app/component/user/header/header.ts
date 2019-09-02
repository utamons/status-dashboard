import {Component} from '@angular/core';
import {StorageService} from '../../../service/storage';

@Component({
  selector: 'app-service-header',
  templateUrl: './header.html',
  styleUrls: ['./header.css']
})
export class HeaderComponent {
  constructor(public storage: StorageService) {}
}
