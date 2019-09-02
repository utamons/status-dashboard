import {Component} from '@angular/core';
import {Subscription, timer} from 'rxjs';
import {Router} from '@angular/router';
import {RestService} from '../../../service/rest';
import {Error} from '../../../model/error';
import {IssueReport} from '../../../model/issueReport';

@Component({
    selector: 'app-report-issue',
    templateUrl: './reportIssue.html',
    styleUrls: ['./reportIssue.css']
})
export class ReportIssueComponent {

    error: Error = null;
    text = '';
    showThanks = false;
    private timerSub: Subscription;


    constructor(private restService: RestService, private router: Router ) {
    }

    submit() {
        if (this.text.trim().length > 0) {
            this.showThanks = true;

            const issueReport: IssueReport = new IssueReport();
            issueReport.reportText = this.text;

            this.restService.postIssueReport(issueReport).subscribe(
                model => {
                    console.log('post issue returned ', model.value);
                    const tm = timer(2000, 2000);
                    this.timerSub = tm.subscribe(() => {
                        this.showThanks = false;
                        this.timerSub.unsubscribe();
                        this.router.navigate(['/home']).catch();
                    });
                },
                error => {
                    this.error = new Error(0, error.error && error.error.message ? error.error.message : error.message);
                }
            );
        }
    }

    // noinspection JSMethodCanBeStatic
    autoGrow(element) {
        element.style.height = '50px';
        element.style.height = (element.scrollHeight) + 'px';
    }

}
