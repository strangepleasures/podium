/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

import './vendor.ts';

import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { Ng2Webstorage } from 'ng2-webstorage';

import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { PodiumGatewaySharedModule, UserRouteAccessService } from './shared';
import { PodiumGatewayAdminModule } from './admin/admin.module';
import { PodiumGatewayAccountModule } from './account/account.module';

import { LayoutRoutingModule } from './layouts';
import { HomeComponent } from './home';
import { DashboardComponent } from './dashboard';

import { customHttpProvider } from './blocks/interceptor/http.provider';
import { PaginationConfig } from './blocks/config/uib-pagination.config';

import {
    PdmMainComponent,
    NavbarComponent,
    FooterComponent,
    ProfileService,
    PageRibbonComponent,
    ActiveMenuDirective,
    ErrorComponent,
    CompletedComponent
} from './layouts';

import { OrganisationService } from './backoffice/modules/organisation/organisation.service';
import { RoleService } from './backoffice/modules/role/role.service';

import { PodiumGatewayRequestModule } from './request/request.module';
import {
    PodiumGatewayBbmriBackofficeModule
} from './backoffice/bbmri/bbmri-backoffice.module';
import { PodiumGatewayOrganisationBackofficeModule } from './backoffice/organisation/organisation-backoffice.module';

@NgModule({
    imports: [
        NgbModule.forRoot(),
        BrowserModule,
        LayoutRoutingModule,
        Ng2Webstorage.forRoot({ prefix: 'pdm', separator: '-'}),
        PodiumGatewaySharedModule.forRoot(),
        PodiumGatewayAdminModule,
        PodiumGatewayRequestModule,
        PodiumGatewayAccountModule,
        PodiumGatewayBbmriBackofficeModule,
        PodiumGatewayOrganisationBackofficeModule

    ],
    declarations: [
        PdmMainComponent,
        HomeComponent,
        DashboardComponent,
        NavbarComponent,
        ErrorComponent,
        CompletedComponent,
        PageRibbonComponent,
        ActiveMenuDirective,
        FooterComponent
    ],
    providers: [
        ProfileService,
        { provide: Window, useValue: window },
        { provide: Document, useValue: document },
        customHttpProvider(),
        PaginationConfig,
        UserRouteAccessService,
        OrganisationService,
        RoleService
    ],
    bootstrap: [ PdmMainComponent ],
    exports: [NgbModule]
})
export class PodiumGatewayAppModule {}
