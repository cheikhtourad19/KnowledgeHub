import { ComponentFixture, TestBed } from '@angular/core/testing';

import { StudentsClasseComponent } from './students.classe.component';

describe('StudentsClasseComponent', () => {
  let component: StudentsClasseComponent;
  let fixture: ComponentFixture<StudentsClasseComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [StudentsClasseComponent]
    })
    .compileComponents();
    
    fixture = TestBed.createComponent(StudentsClasseComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
