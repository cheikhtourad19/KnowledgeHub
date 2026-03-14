import { ComponentFixture, TestBed } from '@angular/core/testing';

import { PfeDetailsViewComponent } from './pfe-details-view.component';

describe('PfeDetailsViewComponent', () => {
  let component: PfeDetailsViewComponent;
  let fixture: ComponentFixture<PfeDetailsViewComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [PfeDetailsViewComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(PfeDetailsViewComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
