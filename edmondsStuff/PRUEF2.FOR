      SUBROUTINE PRUEF2 (IK1,N,NB,KOST,INDEX,TOP,BASIS,MEM,KA,KB,
     *                   SM,Y1,Y2,DPLUS,DMINUS,BB2,BBB)
      IMPLICIT REAL*8 (A,C-H,O-Z)
      IMPLICIT   INTEGER*4 (B,I-M)
      INTEGER*4    NB(IK1),KOST(IK1),INDEX(IK1),TOP
      INTEGER*4    BASIS(IK1),MEM(IK1),KA(IK1),KB(IK1),SM(IK1)
      REAL*8       Y1(IK1),Y2(IK1),DPLUS(IK1),DMINUS(IK1)
C - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -
C *** SCANNING OF NODES BB2 - BBB
C
 9000 KKN2      = BASIS(BB2)
      IF (KKN2.NE.BB2)   GOTO 9010
      D2        = TOP
      YY        = Y1(BB2)
 9040 I         = INDEX(KKN2)
      NN        = INDEX(KKN2+1)-1
      Y         = Y2(KKN2)
      DO 9020 KANT = I,NN
      KKN       = NB(KANT)
      BB        = BASIS(KKN)
      IF (BB.EQ.BB2)    GOTO 9020
      IF (SM(BB).LT.0)   GOTO 9020
      D1        = DPLUS(BB)-Y1(BB)-Y2(KKN)
         D3           = FLOAT(KOST(KANT))
      D3        = D3+D1-YY-Y
      IF (D2.LE.D3)      GOTO 9020
      D2        = D3
      KA(BB2)   = KKN
      KB(BB2)   = KKN2
 9020 CONTINUE
      KKN2      = MEM(KKN2)
      IF (KKN2.NE.BB2)   GOTO 9040
      DMINUS(BB2)= D2
 9010 BB2        = BB2+1
      IF (BB2.LE.BBB)    GOTO 9000
      RETURN
      END
