      SUBROUTINE ERW (IK1,N,BASIS,MEM,KB,Y1,Y2,BB,KKN3)
      IMPLICIT REAL*8 (A,C-H,O-Z)
      IMPLICIT   INTEGER*4 (B,I-M)
      INTEGER*4    BASIS(IK1),MEM(IK1),KB(IK1)
      REAL*8       Y1(IK1),Y2(IK1)
C - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - 
C *** EXPANDING A BLOSSOM
C
      KKN2           = BB
 9800 BB             = KKN2
      KKN4           = KB(BB)
      YY             = Y1(BB)
 9810 BASIS(KKN2)    = BB
      Y2(KKN2)       = Y2(KKN2)-YY
      IF (KKN2.EQ.KKN4)  GOTO 9820
      KKN2           = MEM(KKN2)
                         GOTO 9810
 9820 KKN2           = MEM(KKN4)
      MEM(KKN4)      = BB
      IF (KKN2.NE.KKN3)  GOTO 9800
      RETURN
      END
