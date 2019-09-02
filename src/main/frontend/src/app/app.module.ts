import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {AppComponent} from './app.component';
import {RestService} from './service/rest';
import {AppRoutingModule} from './app-routing.module';
import {HttpClientModule} from '@angular/common/http';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {MainComponent} from './component/user/home/home';
import {StorageService} from './service/storage';
import {HeaderComponent} from './component/user/header/header';
import {AnnouncementComponent} from './component/user/announcement/announcement';
import {EventComponent} from './component/user/event/event';
import {AreasComponent} from './component/user/areas/areas';
import {MenuComponent} from './component/user/menu/menu';
import {HistoryComponent} from './component/user/history/history';
import {ReportIssueComponent} from './component/user/reportIssuse/reportIssue';
import {SubscribeComponent} from './component/user/subscribe/subscribe';
import {LoginComponent} from './component/user/login/login';
import {MaintainerComponent} from './component/maintainer/main/main';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {
    MatBadgeModule,
    MatButtonModule,
    MatCardModule, MatDialogModule,
    MatFormFieldModule,
    MatInputModule,
    MatListModule,
    MatSelectModule
} from '@angular/material';
import {IssueFormComponent} from './component/maintainer/issue/issue';
import {EventUpdatesComponent} from './component/maintainer/updates/updates';
import {AnnouncementFormComponent} from './component/maintainer/announcementForm/announcementForm';
import {ErrorComponent} from './component/error/error';
import {IssueReportsComponent} from './component/maintainer/issueReports/issueReports';
import {ConfirmationPopupComponent} from './component/maintainer/confirmation/confirmation';

/*
   todo Dark theme support?
 */

@NgModule({
    declarations: [
        AppComponent,
        MainComponent,
        HeaderComponent,
        AnnouncementComponent,
        EventComponent,
        AreasComponent,
        MenuComponent,
        HistoryComponent,
        ReportIssueComponent,
        SubscribeComponent,
        LoginComponent,
        MaintainerComponent,
        IssueFormComponent,
        EventUpdatesComponent,
        AnnouncementFormComponent,
        ErrorComponent,
        IssueReportsComponent,
        ConfirmationPopupComponent
    ],
    entryComponents: [
        ConfirmationPopupComponent
    ],
    imports: [
        BrowserModule,
        AppRoutingModule,
        HttpClientModule,
        FormsModule,
        BrowserAnimationsModule,
        MatCardModule,
        MatListModule,
        MatFormFieldModule,
        MatInputModule,
        MatButtonModule,
        MatBadgeModule,
        MatSelectModule,
        ReactiveFormsModule,
        MatDialogModule
    ],
    providers: [RestService, StorageService],
    bootstrap: [AppComponent]
})
export class AppModule {
}
