      SUBROUTINE SCHR (IK1,N,BASIS,MEM,Y1,Y2,BB,KKN)
      IMPLICIT REAL*8 (A,C-H,O-Z)
      IMPLICIT   INTEGER*4 (B,I-M)
      INTEGER*4    BASIS(IK1),MEM(IK1)
      REAL*8 Y1(IK1),Y2(IK1)
C - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
C *** SHRINKING OF A BLOSSOM
C
      KKN1           = KKN
      YY             = Y1(KKN)
 9700 BASIS(KKN)     = BB
      Y2(KKN)        = Y2(KKN)+YY
      KKN2           = MEM(KKN)
      IF (KKN2.EQ.KKN1)  GOTO 9710
      KKN            = KKN2
                         GOTO 9700
 9710 RETURN
      END
