import {NgModule} from '@angular/core';
import {RouterModule, Routes} from '@angular/router';
import {MainComponent} from './component/user/home/home';
import {HistoryComponent} from './component/user/history/history';
import {ReportIssueComponent} from './component/user/reportIssuse/reportIssue';
import {SubscribeComponent} from './component/user/subscribe/subscribe';
import {LoginComponent} from './component/user/login/login';
import {MaintainerComponent} from './component/maintainer/main/main';

const routes: Routes = [
    {path: '', redirectTo: '/home', pathMatch: 'full'},
    {path: 'index.html', redirectTo: '/home', pathMatch: 'full'},
    {path: 'home', component: MainComponent},
    {path: 'history', component: HistoryComponent},
    {path: 'report', component: ReportIssueComponent},
    {path: 'subscribe', component: SubscribeComponent},
    {path: 'login', component: LoginComponent},
    {path: 'maintain', component: MaintainerComponent},
];

@NgModule({
    imports: [RouterModule.forRoot(routes)],
    exports: [RouterModule]
})
export class AppRoutingModule {
}
