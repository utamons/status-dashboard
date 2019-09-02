import {Component} from '@angular/core';
import {StorageService} from '../../../service/storage';

@Component({
  selector: 'app-event',
  templateUrl: './event.html',
  styleUrls: ['./event.css']
})
export class EventComponent {
  constructor(public storage: StorageService) {}
}
