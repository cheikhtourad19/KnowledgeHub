import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PfeGestionComponent } from './pfe.gestion.component';

describe('PfeGestionComponent', () => {
  let component: PfeGestionComponent;
  let fixture: ComponentFixture<PfeGestionComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [PfeGestionComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(PfeGestionComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
