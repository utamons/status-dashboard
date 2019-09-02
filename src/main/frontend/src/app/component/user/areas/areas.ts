import {Component} from '@angular/core';
import {StorageService} from '../../../service/storage';

@Component({
  selector: 'app-areas',
  templateUrl: './areas.html',
  styleUrls: ['./areas.css']
})
export class AreasComponent {
  constructor(public storage: StorageService) {}
}
