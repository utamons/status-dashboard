import {Component} from '@angular/core';
import {StorageService} from '../../../service/storage';

@Component({
  selector: 'app-announcement',
  templateUrl: './announcement.html',
  styleUrls: ['./announcement.css']
})
export class AnnouncementComponent {
  constructor(public storage: StorageService) {}
}
