import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PfeDemandeComponent } from './pfe.demande.component';

describe('PfeDemandeComponent', () => {
  let component: PfeDemandeComponent;
  let fixture: ComponentFixture<PfeDemandeComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PfeDemandeComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(PfeDemandeComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
